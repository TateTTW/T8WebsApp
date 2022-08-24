import {Component, EventEmitter, OnInit, Output} from '@angular/core';

@Component({
  selector: 'admin-content',
  templateUrl: './admin-content.component.html',
  styleUrls: ['./admin-content.component.less']
})
export class AdminContentComponent implements OnInit {

  @Output() refreshTree: EventEmitter<any> = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
  }

}
