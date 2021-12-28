import {Component, EventEmitter, OnInit, Output, ViewChild} from '@angular/core';
import {
  BeforeOpenCloseMenuEventArgs,
  ContextMenuComponent,
  MenuEventArgs,
  MenuItemModel,
  NodeClickEventArgs,
  TreeViewComponent
} from "@syncfusion/ej2-angular-navigations";
import {NodeType, TreeNode} from "./TreeNode";
import {Job, JobAction, JobType} from "../server-dialog/Job";

@Component({
  selector: 'dashboard-tree',
  templateUrl: './dashboard-tree.component.html',
  styleUrls: ['./dashboard-tree.component.less']
})
export class DashboardTreeComponent implements OnInit {
  // View Elements
  @ViewChild ('treeView') treeView!: TreeViewComponent;
  @ViewChild ('contextMenu') contextMenu!: ContextMenuComponent;

  @Output() nodeSelection: EventEmitter<TreeNode> = new EventEmitter<TreeNode>();
  @Output() openAddServer: EventEmitter<Job> = new EventEmitter<Job>();

  public treeData: Object[] = [
    { id: '0', name: 'Servers', expanded: true, hasAttribute:{type: 0},
      subChild: [
        {id: '101', name: 'GurrCannabis', hasAttribute:{type: 1}},
        {id: '102', name: 'DiabloWiki', hasAttribute:{type: 1}}
      ]
    },
    {
      id: '1', name: 'Load Balancers', hasAttribute:{type: 2},
      subChild: [
        {id: '1', name: 'Alex', hasAttribute:{type: 3}}
      ]
    }
  ];

  public treeFields: Object = { dataSource: this.treeData, id: 'id', text: 'name', child: 'subChild', htmlAttributes: 'hasAttribute' };

  public contextMenuItems: MenuItemModel[] = [{ text: 'Add' }];

  constructor() { }

  ngOnInit(): void { }

  public nodeClicked(args: NodeClickEventArgs) {
    let id = args.node.getAttribute('data-uid');
    let type = args.node.getAttribute('type');
    let text = args.node.innerText;

    if(text.includes("\n")){
      text = text.substring(0, text.indexOf("\n"));
    }

    if (args.event.which === 3 && id) {
      this.treeView.selectedNodes = [id];
    }

    if(id && !isNaN(parseInt(id)) && type && !isNaN(parseInt(type))){
      const nodeId = parseInt(id);
      const nodeType = NodeType.findNodeType(parseInt(type));
      const treeNode = new TreeNode(nodeId, text, nodeType);
      this.nodeSelection.emit(treeNode);
    }
  }

  public contextMenuClick(args: MenuEventArgs) {
    let nodeId: string = this.treeView?.selectedNodes[0];
    if(isNaN(parseInt(nodeId))){
      return;
    }

    const nodeType = NodeType.findNodeType(parseInt(nodeId));
    if (args.item.text == "Add") {
      const job: Job = {
        type: nodeType === NodeType.ServerGroup ? JobType.Server : JobType.LoadBalancer,
        action: JobAction.Add,
        vmid: -1
      }
      this.openAddServer.emit(job);
    }
  }

  public beforeOpen(args: BeforeOpenCloseMenuEventArgs) {
    let id: string = this.treeView.selectedNodes[0];
    let type = parseInt(document.querySelector('[data-uid="' + id + '"]')?.getAttribute('type') ?? '-1');

    if (type == NodeType.ServerGroup.id || type === NodeType.BalancerGroup.id) {
      this.contextMenu.showItems(['Add']);
    } else {
      this.contextMenu.hideItems(['Add']);
    }
  }

}
