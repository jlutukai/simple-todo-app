#!/bin/bash

# Install git hooks for the project
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
HOOKS_DIR="$(git rev-parse --show-toplevel)/.git/hooks"

echo "Installing git hooks..."

cp "$SCRIPT_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
cp "$SCRIPT_DIR/pre-push" "$HOOKS_DIR/pre-push"

chmod +x "$HOOKS_DIR/pre-commit"
chmod +x "$HOOKS_DIR/pre-push"

echo "✅ Git hooks installed successfully!"
echo ""
echo "Installed hooks:"
echo "  - pre-commit: Runs spotlessCheck and detekt before each commit"
echo "  - pre-push: Runs spotlessCheck and detekt before each push"
