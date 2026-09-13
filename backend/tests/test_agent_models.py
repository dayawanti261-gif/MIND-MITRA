"""Tests for OpenRouter free-model configuration and fallback."""

import os
import unittest
from unittest.mock import MagicMock, patch

import agent


class ValidateFreeModelTests(unittest.TestCase):
    def test_openrouter_free_accepted(self):
        self.assertEqual(agent.validate_free_model("openrouter/free"), "openrouter/free")

    def test_colon_free_model_accepted(self):
        model = "meta-llama/llama-3.3-70b-instruct:free"
        self.assertEqual(agent.validate_free_model(model), model)

    def test_paid_model_rejected(self):
        with self.assertRaises(RuntimeError) as ctx:
            agent.validate_free_model("openai/gpt-4o")
        self.assertEqual(str(ctx.exception), "INVALID_MODEL")

    def test_empty_model_rejected(self):
        with self.assertRaises(RuntimeError):
            agent.validate_free_model("   ")


class ModelConfigParsingTests(unittest.TestCase):
    def setUp(self):
        self._env = os.environ.copy()

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._env)

    def test_multiple_models_parsed_from_openrouter_models(self):
        os.environ["OPENROUTER_MODELS"] = (
            "openrouter/free,meta-llama/llama-3.3-70b-instruct:free,"
            "google/gemini-2.0-flash-exp:free"
        )
        os.environ.pop("OPENROUTER_MODEL", None)
        models = agent.get_models_for_request()
        self.assertEqual(
            models,
            [
                "openrouter/free",
                "meta-llama/llama-3.3-70b-instruct:free",
                "google/gemini-2.0-flash-exp:free",
            ],
        )

    def test_openrouter_models_preferred_over_legacy(self):
        os.environ["OPENROUTER_MODELS"] = "openrouter/free"
        os.environ["OPENROUTER_MODEL"] = "openai/gpt-4o"
        models = agent.get_models_for_request()
        self.assertEqual(models, ["openrouter/free"])

    def test_legacy_openrouter_model_used_when_models_unset(self):
        os.environ.pop("OPENROUTER_MODELS", None)
        os.environ["OPENROUTER_MODEL"] = "meta-llama/llama-3.3-70b-instruct:free"
        models = agent.get_models_for_request()
        self.assertEqual(models, ["meta-llama/llama-3.3-70b-instruct:free"])


class ModelFallbackTests(unittest.TestCase):
    def setUp(self):
        self._env = os.environ.copy()
        os.environ["OPENROUTER_API_KEY"] = "test-key"
        os.environ["OPENROUTER_MODELS"] = (
            "openrouter/free,meta-llama/llama-3.3-70b-instruct:free"
        )

    def tearDown(self):
        os.environ.clear()
        os.environ.update(self._env)

    @patch("agent.requests.post")
    def test_fallback_from_first_model_to_second(self, mock_post):
        rate_limited = MagicMock()
        rate_limited.status_code = 429

        success = MagicMock()
        success.status_code = 200
        success.json.return_value = {
            "choices": [{"message": {"content": '{"message":"Hi","action":null}'}}]
        }

        mock_post.side_effect = [rate_limited, success]

        result = agent._chat_completion_with_fallback(
            [{"role": "user", "content": "hello"}]
        )

        self.assertEqual(result["choices"][0]["message"]["content"], '{"message":"Hi","action":null}')
        self.assertEqual(mock_post.call_count, 2)
        first_model = mock_post.call_args_list[0].kwargs["json"]["model"]
        second_model = mock_post.call_args_list[1].kwargs["json"]["model"]
        self.assertEqual(first_model, "openrouter/free")
        self.assertEqual(second_model, "meta-llama/llama-3.3-70b-instruct:free")

    @patch("agent.requests.post")
    def test_all_models_failing_raises_all_models_failed(self, mock_post):
        failing = MagicMock()
        failing.status_code = 503
        mock_post.return_value = failing

        with self.assertRaises(RuntimeError) as ctx:
            agent._chat_completion_with_fallback(
                [{"role": "user", "content": "hello"}]
            )

        self.assertEqual(str(ctx.exception), "ALL_MODELS_FAILED")
        self.assertEqual(mock_post.call_count, 2)

    @patch("agent.requests.post")
    def test_auth_error_does_not_fallback(self, mock_post):
        unauthorized = MagicMock()
        unauthorized.status_code = 401
        mock_post.return_value = unauthorized

        with self.assertRaises(RuntimeError) as ctx:
            agent._chat_completion_with_fallback(
                [{"role": "user", "content": "hello"}]
            )

        self.assertEqual(str(ctx.exception), "OPENROUTER_AUTH_ERROR")
        self.assertEqual(mock_post.call_count, 1)


class AgentChatEndpointTests(unittest.TestCase):
    def test_all_models_failed_maps_to_503(self):
        from fastapi import HTTPException
        from main import agent_chat
        from models.schemas import AgentChatRequest

        with patch("agent.is_agent_configured", return_value=True), patch(
            "agent.is_model_configured", return_value=True
        ), patch("agent.run_agent", side_effect=RuntimeError("ALL_MODELS_FAILED")):
            with self.assertRaises(HTTPException) as ctx:
                agent_chat(
                    AgentChatRequest(message="hello", language="en"),
                    caller_uid="user-1",
                )

        self.assertEqual(ctx.exception.status_code, 503)
        self.assertIn("temporarily unavailable", ctx.exception.detail)


if __name__ == "__main__":
    unittest.main()
