#!/usr/bin/env sh

set -exuf -o

GH_PAGES_DIR=gh-pages

removeGhPagesWorktree() {
    echo "Deleting worktree..."
    rm -rf $GH_PAGES_DIR
    git worktree prune
}

git config --global core.sshCommand 'ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no'
git config --global user.name "$GITHUB_GIT_USER_NAME";
git config --global user.email "$GITHUB_GIT_USER_EMAIL";

echo "Fetching gh-pages branch..."
# In case of not fetched branch
# e.g. Teamcity fetches all branches only with `teamcity.git.fetchAllHeads` enabled
git fetch origin gh-pages
git show-ref origin/gh-pages

removeGhPagesWorktree

echo "Checking out gh-pages branch into a temporary directory..."
git worktree add -B gh-pages $GH_PAGES_DIR origin/gh-pages

echo "Generating site..."
mkdocs build --clean --strict --config-file docs/mkdocs.yml --site-dir ../$GH_PAGES_DIR --verbose
cd $GH_PAGES_DIR

git add --all
git status

echo "Pushing changes..."
# Amend history because we don't want to bloat it by generated files
git commit --amend -m "Auto-generated files" && git push --force-with-lease

cd ..
removeGhPagesWorktree
