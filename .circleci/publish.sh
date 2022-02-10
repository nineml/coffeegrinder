#!/bin/bash

if [ -z "$CIRCLE_TAG" ]; then
    echo "Only tagged commits are published"
    exit 0
fi

echo Publishing not yet implemented

