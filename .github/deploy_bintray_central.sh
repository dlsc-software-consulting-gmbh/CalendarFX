#!/usr/bin/env bash
echo "Copy settings.xml for deployment"
cp .github/.travis.settings.xml $HOME/.m2/settings.xml
echo "Deploy to Bintray"
mvn deploy -DskipTests
echo "Sync to Maven Central"
result=$(curl -X POST -u $BINTRAY_USER:$BINTRAY_API_KEY https://api.bintray.com/maven_central_sync/$BINTRAY_SUBJECT/$BINTRAY_REPO/$BINTRAY_PACKAGE/versions/$TRAVIS_TAG)
if [[ $result ==  *"Successfully synced and closed repo."* ]]
then
	echo Successfully synced to Maven Central
	exit 0
else
	echo Failed sync to Maven Central: $result
	exit 1
fi
