import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'net-graph',
  templateUrl: './net-graph.component.html',
  styleUrls: ['./net-graph.component.less']
})
export class NetGraphComponent implements OnInit {
  // Input variables
  @Input() netData: NetData = {unit: 'Kb', netIn: [], netOut: []};
  //Initializing Primary X Axis
  public primaryXAxis: Object = {
    valueType: 'DateTime',
    labelFormat: 'h:mm',
    majorGridLines: {width: 0},
    intervalType: 'Minutes',
    edgeLabelPlacement: 'Shift'
  };
  //Initializing Primary Y Axis
  public primaryYAxis: Object = {
    title: 'Kb',
    labelFormat: '{value} Kb',
    lineStyle: {width: 0},
    majorTickLines: {width: 0},
    minorTickLines: {width: 0}
  };

  public marker: Object = {
    visible: false
  };

  public chartArea: Object = {
    border: {
      width: 0
    }
  };

  tooltip = {enable: true, header: '<b>${series.name}</b>', format: '<b>${point.x} : ${point.y}</b>'};

  constructor() {
  }

  ngOnInit(): void {
  }

  axisLabelRender(event: any) {
    if(event.axis.name == 'primaryYAxis'){
      event.axis.title = this.netData.unit;
      event.axis.labelFormat = '{value} ' + this.netData.unit;
    }
  }
}

export interface NetData {
  unit: string,
  netIn: {x:Date,y:string}[],
  netOut: {x:Date,y:string}[]
}
