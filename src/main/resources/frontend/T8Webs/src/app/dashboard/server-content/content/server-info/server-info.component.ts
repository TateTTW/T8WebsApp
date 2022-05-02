import {Component, Input, OnInit} from '@angular/core';
import {NodeType, TreeNode} from "../../../dashboard-tree/TreeNode";

@Component({
  selector: 'server-info',
  templateUrl: './server-info.component.html',
  styleUrls: ['./server-info.component.less']
})
export class ServerInfoComponent implements OnInit {

  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.None);

  constructor() { }

  ngOnInit(): void {
  }

}
