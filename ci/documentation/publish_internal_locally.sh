#!/usr/bin/env sh

set -exuf -o

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
# shellcheck source=ci/_environment.sh
source "$SCRIPT_DIR"/../_environment.sh

GH_PAGES_DIR=gh-pages

removeGhPagesWorktree() {
    echo "Deleting worktree..."
    rm -rf $GH_PAGES_DIR
    git worktree prune
}

echo "Fetching gh-pages branch..."
# In case of not fetched branch
# e.g. Teamcity fetches all branches only with `teamcity.git.fetchAllHeads` enabled
git fetch origin gh-pages
git show-ref origin/gh-pages

removeGhPagesWorktree

echo "Checking out gh-pages branch into a temporary directory..."
git worktree add -B gh-pages $GH_PAGES_DIR origin/gh-pages

echo "Generating site..."
# NB: Can't use strict mode with serving. Use lint.sh for checking.
docker run --rm -it \
    -p 8000:8000 \
    --volume "$SCRIPT_DIR/../..":/app \
    -w="/app" \
    "${DOCUMENTATION_IMAGE}" \
    mkdocs build --clean --strict --config-file docs/mkdocs.yml --site-dir ../$GH_PAGES_DIR --verbose

cd $GH_PAGES_DIR

git add --all
git status

echo "Pushing changes..."
# Amend history because we don't want to bloat it by generated files
git commit --amend -m "Auto-generated files" && git push --force-with-lease

cd ..
removeGhPagesWorktree
