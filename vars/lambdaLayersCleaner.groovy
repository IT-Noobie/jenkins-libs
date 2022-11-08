#!/usr/bin/env groovy

// Examples:
// - Production execution: lambdaLayerCleaner('pro', 4)
// - Staging execution: lambdaLayerCleaner('stg', 4)
// - Develop execution: lambdaLayerCleaner('dev', 4)

def call(ENVIRONMENT, MAXVERSIONS) {
sh """  #!/bin/bash
  set -x
  if [ \${ENVIRONMENT} != \'pro\' ] && [ \${ENVIRONMENT} != \'stg\' ] && [ \${ENVIRONMENT} != \'dev\' ];
  then
    exit 1
  fi
  layersName=( $(aws lambda list-layers | jq -r ".Layers[].LayerName") )
  for layer in ${layersName[@]}
  do

    if [[ $layer != "\${ENVIRONMENT}-core-unicorn"* ]] && [[ $layer != "\${ENVIRONMENT}-core-analysis"* ]] && [[ $layer != "\${ENVIRONMENT}-core-setup"* ]];
    then
      continue
    fi

    echo "List of all layers versions of: ${layer}"
    lambdaLayerVersions=( $(aws lambda list-layer-versions --layer-name $layer | jq -r ".LayerVersions[].LayerVersionArn") )
    for i in ${lambdaLayerVersions[@]}
    do
      echo $i
    done

    while [ ${#lambdaLayerVersions[@]} -gt ${MAXVERSIONS} ]
    do
      version=$(echo ${lambdaLayerVersions[${#lambdaLayerVersions[@]}-1]} | cut -d: -f8)
      layer_name=$(echo ${lambdaLayerVersions[${#lambdaLayerVersions[@]}-1]} | cut -d: -f7)
      echo "Proceeding to delete layer ${layer_name}:${version}"
      #aws lambda delete-layer-version --layer-name ${layer_name} --version-number ${version}
      echo "The following lambda layer has been deleted: ${layer_name}:${version}\n"
      unset "lambdaLayerVersions[${#lambdaLayerVersions[@]}-1]"
    done
  done"""
}

