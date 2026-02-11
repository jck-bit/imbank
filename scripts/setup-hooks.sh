#!/bin/bash

# Setup Git Hooks
# This script installs the project's git hooks to your local .git/hooks directory

set -e

HOOKS_DIR=".githooks"
GIT_HOOKS_DIR=".git/hooks"

echo ""

# Check if .githooks directory exists
if [ ! -d "$HOOKS_DIR" ]; then
    echo "Make sure you're running this from the project root"
    exit 1
fi

# Check if .git directory exists
if [ ! -d ".git" ]; then
    echo "Make sure you're in the project root with a .git directory"
    exit 1
fi

# Create .git/hooks if it doesn't exist
mkdir -p "$GIT_HOOKS_DIR"

# Install each hook
HOOKS_INSTALLED=0
for hook in "$HOOKS_DIR"/*; do
    if [ -f "$hook" ]; then
        hook_name=$(basename "$hook")
        target="$GIT_HOOKS_DIR/$hook_name"

        # Create symlink
        ln -sf "../../$HOOKS_DIR/$hook_name" "$target"
        chmod +x "$hook"

        HOOKS_INSTALLED=$((HOOKS_INSTALLED + 1))
    fi
done

echo ""
if [ $HOOKS_INSTALLED -eq 0 ]; then
    echo "⚠️  No hooks found in $HOOKS_DIR"
else
    echo "🎉 Successfully installed $HOOKS_INSTALLED hook(s)!"
    echo ""
    echo "📋 Installed hooks:"
    ls -la "$GIT_HOOKS_DIR" | grep "^l" | awk '{print "   - " $9}'
fi

echo ""
echo "✅ Git hooks setup complete!"
