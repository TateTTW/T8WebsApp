import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.less']
})
export class AdminUsersComponent implements OnInit {

  public mediaQuery: string = 'max-width: 1230px';
  public draggableHandle: string = ".e-panel-header";

  constructor() { }

  ngOnInit(): void {
  }

}
