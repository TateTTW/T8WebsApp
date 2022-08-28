import {Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {
  BeforeOpenCloseMenuEventArgs,
  ContextMenuComponent,
  MenuEventArgs,
  MenuItemModel,
  NodeClickEventArgs,
  TreeViewComponent
} from "@syncfusion/ej2-angular-navigations";
import {NodeType, TreeNode} from "./TreeNode";
import {Job, JobType} from "../server-dialog/Job";
import {DashboardService} from "../dashboard.service";
import {Subscription} from "rxjs";

@Component({
  selector: 'dashboard-tree',
  templateUrl: './dashboard-tree.component.html',
  styleUrls: ['./dashboard-tree.component.less']
})
export class DashboardTreeComponent implements OnInit, OnDestroy {
  // Subscriptions
  private getServersSub: Subscription | undefined;
  private serverStatusSub: Subscription | undefined;
  // View Elements
  @ViewChild ('treeView') treeView!: TreeViewComponent;
  @ViewChild ('contextMenu') contextMenu!: ContextMenuComponent;

  @Output() nodeSelection: EventEmitter<TreeNode> = new EventEmitter<TreeNode>();
  @Output() doJob: EventEmitter<Job> = new EventEmitter<Job>();

  vmItems = ['Start','Stop','Reboot'];
  groupItems = ['Add'];

  // group id must match hasAttribute{ type } which represents nodeType
  public treeData: Object[] = [];
  private _treeFields: Object | undefined;
  get treeFields(): Object {
    return { dataSource: this.treeData, id: 'id', text: 'name', child: 'subChild', htmlAttributes: 'hasAttributes' };
  }

  // public treeData: Object[] = [{
  //   "id": "0",
  //   "name": "Servers",
  //   "expanded": true,
  //   "hasAttributes": {
  //     "type": 0
  //   },
  //   "subChild": [{
  //     "id": "128",
  //     "name": "T8Server1",
  //     "hasAttributes": {
  //       "type": 1,
  //       "status": ""
  //     }
  //   }]
  // }]

  //[
  //   { id: '0', name: 'Servers', expanded: true, hasAttribute:{type: 0},
  //     subChild: [
  //       {id: '101', name: 'GurrCannabis', hasAttribute:{type: 1}},
  //       {id: '102', name: 'DiabloWiki', hasAttribute:{type: 1}}
  //     ]
  //   },
  //   {
  //     id: '2', name: 'Load Balancers', hasAttribute:{type: 2},
  //     subChild: [
  //       {id: '201', name: 'Alex', hasAttribute:{type: 3}}
  //     ]
  //   }
  // ];

  public contextMenuItems: MenuItemModel[] = [
    {
      text: 'Add',
      iconCss: 'fa fa-plus'
    },
    {
      text: 'Loading',
      iconCss: 'fa fa-spinner fa-pulse fa-fw'
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
      iconCss: 'fa fa-sync-alt'
    }
  ];

  constructor(private dashboardService: DashboardService) { }

  ngOnInit(): void {
    this.refresh();
  }

  ngOnDestroy(): void {
    this.getServersSub?.unsubscribe();
    this.serverStatusSub?.unsubscribe();
  }

  public refresh(job?: Job): void {
    //this.refreshHandler(this.treeData, job)
    this.getServersSub = this.dashboardService.getTree().subscribe(
      data => this.refreshHandler(data, job),
      error => console.log(error)
    );
  }

  private refreshHandler(data: any[], job?: Job) {
    this.treeData = data ?? [];

    setTimeout(()=> {
      if (job && job.vmid > 0) {
        const node = this.treeView.getNode(job.vmid.toString());
        if (node) {
          this.treeView.selectedNodes = [job.vmid.toString()];

          const nodeType = NodeType.findNodeTypeByName(job.type);
          const treeNode = new TreeNode(job.vmid, node['text']?.toString() ?? "", "", nodeType);
          this.nodeSelection.emit(treeNode);
        }
      }
    },2000);
  }

  private getServerStatus(vmid: number){
    this.serverStatusSub = this.dashboardService.getServerStatus(vmid).subscribe(
      data => this.serverStatusSuccess(data),
      error => this.serverStatusFail(error)
    )
  }

  private serverStatusSuccess(data: any | undefined) {
    const status = data?.status ?? '';

    this.contextMenu.hideItems(['Loading']);
    this.contextMenu.showItems(this.vmItems);

    if (status == 'stopped') {
      this.contextMenu.enableItems(['Stop','Reboot'], false);
      this.contextMenu.enableItems(['Start','Edit'], true);
    } else if (status == 'running'){
      this.contextMenu.enableItems(['Start','Edit'], false);
      this.contextMenu.enableItems(['Stop','Reboot'], true);
    } else {
      this.contextMenu.enableItems(this.vmItems, false);
    }
  }

  private serverStatusFail(error: any) {
    this.contextMenu.enableItems(this.vmItems, false);
    this.contextMenu.hideItems(['Loading']);
    this.contextMenu.showItems(this.vmItems);
  }

  public nodeClicked(args: NodeClickEventArgs) {
    const id = args.node.getAttribute('data-uid');
    if (id && args.event.which === 3) {
      this.treeView.selectedNodes = [id];
    }
  }

  public contextMenuClick(args: MenuEventArgs) {
    let nodeIdStr: string = this.treeView?.selectedNodes[0];
    if (args.item.text && isNaN(parseInt(nodeIdStr))) {
      return;
    }
    const node = this.treeView.getNode(nodeIdStr);
    const nodeId = parseInt(nodeIdStr);

    let nodeType = NodeType.NONE;

    if (!node['parentID']) {
      nodeType = NodeType.findNodeTypeById(nodeId);
    } else if (!isNaN(parseInt(<string>node['parentID']))) {
      nodeType = NodeType.findNodeTypeById(parseInt(<string>node['parentID']));
    }

    this.doJob.emit({
      type: (nodeType == NodeType.SERVER_GROUP) ? JobType.Server : JobType.LoadBalancer,
      action: Job.findJobAction(args.item.text),
      vmid: (nodeId !== NodeType.SERVER_GROUP.id && nodeId !== NodeType.BALANCER_GROUP.id) ? nodeId : -1
    });
  }

  public beforeOpen(args: BeforeOpenCloseMenuEventArgs) {
    let id: string = this.treeView.selectedNodes[0];
    let type = parseInt(document.querySelector('[data-uid="' + id + '"]')?.getAttribute('type') ?? '-1');

    if (type == NodeType.SERVER_GROUP.id || type === NodeType.BALANCER_GROUP.id) {
      this.contextMenu.showItems(this.groupItems);
      this.contextMenu.hideItems(this.vmItems.concat(['Loading']));
    } else if (type == NodeType.SERVER.id) {
      this.contextMenu.hideItems(this.groupItems.concat(this.vmItems));
      this.contextMenu.showItems(['Loading']);

      this.getServerStatus(parseInt(id));
    } else {
      args.cancel = true;
    }
  }

  nodeSelecting(args: any) {
    const id = args.node.getAttribute('data-uid');
    const type = args.node.getAttribute('type');
    const status = args.node.getAttribute('status') ?? '';
    let text = args.node.innerText;

    if (text.includes("\n")) {
      text = text.substring(0, text.indexOf("\n"));
    }

    if (id && !isNaN(parseInt(id)) && type && !isNaN(parseInt(type))) {
      const nodeId = parseInt(id);
      const nodeType = NodeType.findNodeTypeById(parseInt(type));
      const treeNode = new TreeNode(nodeId, text, status, nodeType);
      this.nodeSelection.emit(treeNode);
    }
  }
}
