import * as React from 'react';
// @ts-ignore
import plantumlEncoder from 'plantuml-encoder';

const PlantUML= (props) => {
  const encode = plantumlEncoder.encode(props.src);
  const url = `http://www.plantuml.com/plantuml/svg/${encode}`;

  return <>
    <img alt={props.alt} src={url}/>
  </>;
};

export default PlantUML;
