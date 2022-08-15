import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'ram-graph',
  templateUrl: './ram-graph.component.html',
  styleUrls: ['./ram-graph.component.less']
})
export class RamGraphComponent implements OnInit {
  // Input variables
  @Input() ramData: {x:Date,y:string}[] = [];
  //Initializing Primary X Axis
  public primaryXAxis: Object = {
    valueType: 'DateTime',
    labelFormat: 'h:mm',
    majorGridLines: { width: 0 },
    intervalType: 'Minutes',
    edgeLabelPlacement: 'Shift'
  };
  //Initializing Primary Y Axis
  public primaryYAxis: Object = {
    title: 'Mb',
    labelFormat: '{value} Mb',
    minimum: 0,
    lineStyle: { width: 0 },
    majorTickLines: { width: 0 },
    minorTickLines: { width: 0 }
  };

  public marker: Object = {
    visible: false
  };

  public chartArea: Object = {
    border: {
      width: 0
    }
  };

  tooltip = { enable: true, header: '<b>${series.name}</b>', format: '<b>${point.x} : ${point.y}</b>' };

  constructor() { }

  ngOnInit(): void {
  }

}
