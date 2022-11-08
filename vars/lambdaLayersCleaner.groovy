#!/usr/bin/env groovy

// Examples:
// - Production execution: lambdaLayerCleaner('pro', 4)
// - Staging execution: lambdaLayerCleaner('stg', 4)
// - Develop execution: lambdaLayerCleaner('dev', 4)

def call(environment, maxVersions) {

sh """#!/bin/bash
  echo ${environment}
  if [ ${environment} != 'pro' ] && [ ${environment} != 'stg' ] && [ ${environment} != 'dev' ];
  then
    exit 1
  fi
  layersName=( \$(aws lambda list-layers | jq -r ".Layers[].LayerName") )

  for layer in "${layersName[@]}"; do echo $layer; done
"""
}

