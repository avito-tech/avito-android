#!/usr/bin/env bash

# Build and publish documentation to github pages

set -euf -o pipefail

if [ "$(git status -s)" ]
then
    echo "The working directory is dirty. Please commit any pending changes."
    # TODO: fail
    # exit 1;
fi

echo "Deleting old publication"
rm -rf public
mkdir public
git worktree prune
rm -rf .git/worktrees/public/

echo "Checking out gh-pages branch into public/ directory"
git worktree add -B gh-pages public origin/gh-pages

echo "Generating site"
cd wiki/
docker build --tag android/wiki/local .
cd ..
docker run --rm \
        --volume "$(pwd)"/public:/app/public \
        --entrypoint hugo \
        android/wiki/local:latest \
        --cleanDestinationDir --minify --theme book

# TODO: use machine github user
# echo "Updating gh-pages branch"
# cd public && git add --all && git commit --amend -m "generated files" && git push --force-with-lease
