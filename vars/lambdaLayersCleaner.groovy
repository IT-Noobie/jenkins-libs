#!/usr/bin/env groovy

// Examples:
// - Production execution: lambdaLayerCleaner('pro', 4)
// - Staging execution: lambdaLayerCleaner('stg', 4)
// - Develop execution: lambdaLayerCleaner('dev', 4)

def call(environment, maxVersions) {
  //env.environment = environment
  //env.maxVersions = maxVersions

sh '''#!/bin/bash
  echo ${environment}
'''
}

