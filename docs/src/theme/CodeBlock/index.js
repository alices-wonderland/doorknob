/**
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import React from 'react';
import OriginalCodeBlock from '@theme-init/CodeBlock';
import plantumlEncoder from "plantuml-encoder";

export default function CodeBlock(props) {
  const encode = plantumlEncoder.encode(props.children);
  const url = `https://www.plantuml.com/plantuml/svg/${encode}`;

  return props.className?.includes("language-plantuml") ? <img alt={props.alt} src={url}/> :
    <OriginalCodeBlock {...props} />;
};
