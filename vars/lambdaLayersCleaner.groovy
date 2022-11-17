#!/usr/bin/env groovy

// Examples:
// - Production execution: lambdaLayerCleaner('pro', 4)
// - Staging execution: lambdaLayerCleaner('stg', 4)
// - Develop execution: lambdaLayerCleaner('dev', 4)

def call(ENVIRONMENT, MAXVERSIONS) {
  env.ENVIRONMENT = ENVIRONMENT
  env.MAXVERSIONS = MAXVERSIONS

  sh '''#!/bin/bash
    set +x
    if [ ${ENVIRONMENT} != 'pro' ] && [ ${ENVIRONMENT} != 'stg' ] && [ ${ENVIRONMENT} != 'dev' ];
    then
      exit 1
    fi
    layersName=( $(aws lambda list-layers | jq -r ".Layers[].LayerName") )
    for layer in ${layersName[@]}
    do
      echo "List of all layers versions of: ${layer}"
      lambdaLayerVersions=( $(aws lambda list-layer-versions --layer-name $layer | jq -r ".LayerVersions[].LayerVersionArn") )
      for i in ${lambdaLayerVersions[@]}
      do
        echo $i
      done

      layersToDelete=()

      while [ ${#lambdaLayerVersions[@]} -gt ${MAXVERSIONS} ]
      do
        layerNameVersion=$(echo ${lambdaLayerVersions[${#lambdaLayerVersions[@]}-1]} | cut -d: -f7-8)
        layersToDelete+=(${layerNameVersion})

        unset "lambdaLayerVersions[${#lambdaLayerVersions[@]}-1]"
      done

      echo "The following layers will be deleted: ${layersToDelete[*]}"
      for layer in ${layersToDelete[@]} {
        layer_name=$(echo $layer | cut -d: -f1)
        version=$(echo $version| cut -d: -f2)
        #aws lambda delete-layer-version --layer-name ${layer_name} --version-number ${version} 
        echo "The following lamda layer version has been deleted: ${layer_name}:${version}" 
      }
    done
  '''
}

