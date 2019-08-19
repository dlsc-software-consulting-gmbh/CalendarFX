#!/bin/bash

# Script will zip the javadoc for upload to the release later, generate the changelog and increment the version in the pom.xml file to the one of the tag

echo TRAVIS_TAG=$TRAVIS_TAG
echo TAG_BRANCH=$TAG_BRANCH

if [[ "$TRAVIS_PULL_REQUEST" != "false" || ("$TAG_BRANCH" != "master" && "$TAG_BRANCH" != "master-11") || "$TRAVIS_TAG" == "" ]]
then
  echo "No tag was made from master or master-11, skipping deployment preparation."
  exit 0
fi

rev=$(git rev-parse --short HEAD)

echo "Install Dependencies for Changelog Generation"
gem install rack -v 1.6.4
gem install github_changelog_generator
echo "Finished Install Dependencies for Changelog Generation"

git config user.name "Travis CI"
git config user.email "build@travis-ci.org"

echo "Prepare Git for Commits"
git remote add upstream "https://$GITHUB_TOKEN@github.com/$TRAVIS_REPO_SLUG.git"
git fetch upstream
git checkout $TAG_BRANCH
echo "Finished Prepare Git for Commits"

echo "Generate Changelog"
github_changelog_generator -t $GITHUB_TOKEN
git add -A CHANGELOG.md
git commit -m "update changelog at ${rev}"
echo "Finished Generate Changelog"

echo "Increment Version"
mvn versions:set -DnewVersion=$TRAVIS_TAG -DoldVersion=* -DgroupId=* -DartifactId=*
# increment version of children modules
mvn versions:update-child-modules
git commit -am "increment version to ${TRAVIS_TAG}"
echo "Finished Increment Version"

echo "Push commits"
git push upstream $TAG_BRANCH
echo "Finished Push commits"

echo "Running mvn package"
mvn package -DskipTests
echo "Making zip of javadoc"
cd ${TRAVIS_BUILD_DIR}/CalendarFXView/target/apidocs
zip -r ${TRAVIS_BUILD_DIR}/javadoc.zip .
