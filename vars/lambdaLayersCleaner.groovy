#!/usr/bin/env groovy

// Examples:
// - Production execution: lambdaLayerCleaner('pro', 4)
// - Staging execution: lambdaLayerCleaner('stg', 4)
// - Develop execution: lambdaLayerCleaner('dev', 4)

def call(MAXVERSIONS) {
  env.MAXVERSIONS = MAXVERSIONS

  sh '''#!/bin/bash
  set +x
  layersName=( $(aws lambda list-layers | jq -r ".Layers[].LayerName") )
  for layer in ${layersName[@]}
  do
    lambdaLayerVersions=( $(aws lambda list-layer-versions --layer-name $layer | jq -r ".LayerVersions[].LayerVersionArn") )

    if [[ ${#lambdaLayerVersions[@]} -le ${MAXVERSIONS} ]]
    then
      echo "Layer '${layer}' doesn't surpass the version limit"
      continue
    fi

    printf "\n-----------------------------------------------------\n\n"
    echo "List of all layers versions of: ${layer}"
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

    echo ""
    echo "The following layers will be deleted: ${layersToDelete[*]}"

    for layer in ${layersToDelete[@]}
    do
      layer_name=$(echo ${layer} | cut -d: -f1)
      version=$(echo ${layer}| cut -d: -f2)
      aws lambda delete-layer-version --layer-name ${layer_name} --version-number ${version}
      echo "The following lambda layer version has been deleted: ${layer_name}:${version}"
    done
    printf "\n-----------------------------------------------------\n\n"
  done
  '''
}

