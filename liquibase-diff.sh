#!/bin/bash

selected_profile="liquibase-pcl"
current_timestamp=$(date +"%Y-%m-%dT%H.%M.%S")
current_git_branch=$(git branch --show-current)

current_git_branch=$( [[ "$current_git_branch" == *"/"* ]] && echo "${current_git_branch#*"/"}" || echo "$current_git_branch" )

echo "Selected profile: $selected_profile"

mvn clean install -Dmaven.test.skip=true && \
mvn -pl rest liquibase:diff -P ${selected_profile} -Dfilename=${current_timestamp}-${current_git_branch}
