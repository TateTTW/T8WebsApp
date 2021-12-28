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
  @Output() doJob: EventEmitter<Job> = new EventEmitter<Job>();

  // group id must match hasAttribute{ type } which represents nodeType
  public treeData: Object[] = [
    { id: '0', name: 'Servers', expanded: true, hasAttribute:{type: 0},
      subChild: [
        {id: '101', name: 'GurrCannabis', hasAttribute:{type: 1}},
        {id: '102', name: 'DiabloWiki', hasAttribute:{type: 1}}
      ]
    },
    {
      id: '2', name: 'Load Balancers', hasAttribute:{type: 2},
      subChild: [
        {id: '201', name: 'Alex', hasAttribute:{type: 3}}
      ]
    }
  ];

  public treeFields: Object = { dataSource: this.treeData, id: 'id', text: 'name', child: 'subChild', htmlAttributes: 'hasAttribute' };

  public contextMenuItems: MenuItemModel[] = [
    {
      text: 'Add',
      iconCss: 'fa fa-plus'
    },
    {
      text: 'Start',
      iconCss: 'fa fa-play'
    },
    {
      text: 'Stop',
      iconCss: 'fa fa-stop'
    },
    {
      text: 'Reboot',
      iconCss: 'fa fa-refresh'
    },
    {
      text: 'Edit',
      iconCss: 'fa fa-gear',
      items: [
        {
          text: 'Rename',
          iconCss: 'fa fa-pencil'
        },
        {
          text: 'Delete',
          iconCss: 'fa fa-trash'
        }
      ]
    }
  ];

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
    let nodeIdStr: string = this.treeView?.selectedNodes[0];
    if(args.item.text && isNaN(parseInt(nodeIdStr))){
      return;
    }
    const node = this.treeView.getNode(nodeIdStr);
    const nodeId = parseInt(nodeIdStr);

    let nodeType = NodeType.None;

    if(!node['parentID']){
      nodeType = NodeType.findNodeType(nodeId);
    } else if (!isNaN(parseInt(<string>node['parentID']))) {
      nodeType = NodeType.findNodeType(parseInt(<string>node['parentID']));
    }

    this.doJob.emit({
      type: (nodeType == NodeType.ServerGroup) ? JobType.Server : JobType.LoadBalancer,
      action: Job.findJobAction(args.item.text),
      vmid: (nodeId !== NodeType.ServerGroup.id && nodeId !== NodeType.BalancerGroup.id) ? nodeId : -1
    });
  }

  public beforeOpen(args: BeforeOpenCloseMenuEventArgs) {
    let id: string = this.treeView.selectedNodes[0];
    let type = parseInt(document.querySelector('[data-uid="' + id + '"]')?.getAttribute('type') ?? '-1');

    const vmItems = ['Start','Stop','Reboot','Edit'];
    const groupItems = ['Add'];

    if (type == NodeType.ServerGroup.id || type === NodeType.BalancerGroup.id) {
      this.contextMenu.showItems(groupItems);
      this.contextMenu.hideItems(vmItems);
    } else {
      this.contextMenu.showItems(vmItems);
      this.contextMenu.hideItems(groupItems);
    }
  }

}
