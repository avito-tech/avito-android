#!/usr/bin/env bash

set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
SUBPROJECTS_PROPERTIES="$DIR/../subprojects/gradle.properties"
ROOT_PROPERTIES="$DIR/../gradle.properties"
DRY_RUN=0

for arg in "$@"
do
    case $arg in
        -d|--dry-run)
        DRY_RUN=1
        shift # Remove --dry-run from processing
        ;;
    esac
done

function prop {
    grep "${2}" "${1}"|cut -d'=' -f2
}

# TODO run in docker to preinstall gh (reuse local gh config should be possible)

PREVIOUS_RELEASE_TAG=$(prop "$ROOT_PROPERTIES" 'infraVersion')
RELEASE_TAG=$(prop "$SUBPROJECTS_PROPERTIES" 'projectVersion')

# TODO check if already created

if [ $DRY_RUN != 1 ]; then
    echo "Creating release draft $RELEASE_TAG..."

    CURRENT_BRANCH=$(git branch --show-current)
    if [ "$CURRENT_BRANCH" != "$RELEASE_TAG" ]; then
        echo "ERROR: Script should be run on release branch: $RELEASE_TAG"
        echo "ERROR: Current branch: $CURRENT_BRANCH"
        exit 1
    fi
else
    echo "Dry run release draft $RELEASE_TAG"
fi

if [ $DRY_RUN != 1 ]; then
    git tag "$RELEASE_TAG"
    git push origin --tags
fi

DRAFT_TEMPLATE="## Features
## Fixes
## Meta

Commits between ${PREVIOUS_RELEASE_TAG} and ${RELEASE_TAG}:
$(git log --pretty=format:"%s" "$PREVIOUS_RELEASE_TAG"..."$RELEASE_TAG")
"

if [ $DRY_RUN != 1 ]; then
    gh release create "$RELEASE_TAG" \
        --target "$RELEASE_TAG" \
        --title "$RELEASE_TAG" \
        --draft \
        --notes "$DRAFT_TEMPLATE"
else
    echo "Draft template:"
    echo "---------------"
    echo "$DRAFT_TEMPLATE"
fi
