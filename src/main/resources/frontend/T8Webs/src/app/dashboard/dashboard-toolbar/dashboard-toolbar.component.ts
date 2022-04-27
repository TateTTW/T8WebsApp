import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {ClickEventArgs, ItemModel, ToolbarComponent} from "@syncfusion/ej2-angular-navigations";
import {ItemModel as ButtonItemModel} from '@syncfusion/ej2-angular-splitbuttons';
import {User} from "../dto/user";
import {NodeType, TreeNode} from "../dashboard-tree/TreeNode";
import {TemplateBinding} from "@angular/compiler";
import {Job, JobType} from "../server-dialog/Job";
import {DashboardService} from "../dashboard.service";

@Component({
  selector: 'dashboard-toolbar',
  templateUrl: './dashboard-toolbar.component.html',
  styleUrls: ['./dashboard-toolbar.component.less']
})
export class DashboardToolbarComponent implements OnInit, AfterViewInit, OnChanges {
  // View elements
  @ViewChild('toolbar') toolbar?: ToolbarComponent;
  @ViewChild('loginAndOut') loginAndOut?: TemplateBinding;
  //@ViewChild('title') title?: HTMLElement;

  @Input() user: User | undefined;
  @Input() selectedTreeNode = new TreeNode(-1, 'Dashboard', '', NodeType.None);
  @Output() doJob: EventEmitter<Job> = new EventEmitter<Job>();

  runningItems = ['Stop','Reboot'];
  stoppedItems = ['Start','#editSubmenu']

  public editMenuItems: ButtonItemModel[] = [
    {
      text: 'Rename',
      iconCss: 'fa fa-pencil'
    },
    {
      text: 'Delete',
      iconCss: 'fa fa-trash'
    }
  ];

  public userMenuItems: ItemModel[] = [{text: 'Sign Out'}];

  public menuFields: Object = {
    text: ['header', 'text', 'value'],
    children: ['subItems', 'options']
  };

  constructor() { }

  ngOnInit(): void { }

  ngAfterViewInit(): void { }

  ngOnChanges(changes: SimpleChanges): void {
    const selectedTreeNode = changes['selectedTreeNode'];
    if(this.toolbar && selectedTreeNode && selectedTreeNode.currentValue) {
      const status = selectedTreeNode.currentValue.status;
      const nodeType = selectedTreeNode.currentValue.type;
      const isGroup = nodeType === NodeType.ServerGroup || nodeType === NodeType.BalancerGroup;
      const validItemsArr = status == 'running' ? this.runningItems : status == 'stopped' ? this.stoppedItems : [];

      let index = 0;
      this.toolbar?.items.forEach((item: ItemModel) => {
        if(nodeType == NodeType.None && item.cssClass !== 'persist'){
          this.toolbar?.hideItem(index, true);
        }
        else if(item.cssClass === 'group')
        {
          this.toolbar?.hideItem(index, !isGroup);
        }
        else if (item.cssClass === 'item')
        {
          const enable = validItemsArr.includes(item.text ?? '') || validItemsArr.includes(item.template?.toString() ?? '');
          this.toolbar?.enableItems(index, enable);
          this.toolbar?.hideItem(index, isGroup);
        }
        index++;
      });
    }
  }

  public created(): void {
    this.toolbar?.refreshOverflow();
  }

  public userMenuSelect(args: Event): void {
    this.logout();
  }

  public login(){
    window.location.replace("/oauth2/authorization/google");
  }

  public logout() {
    window.location.replace("/logout");
  }

  toolbarClick(args: ClickEventArgs) {
    if(args.item.text == ""){
      return;
    }

    this.doJob.emit({
      type: (this.selectedTreeNode.type == NodeType.ServerGroup || this.selectedTreeNode.type == NodeType.Server) ? JobType.Server : JobType.LoadBalancer,
      action: Job.findJobAction(args.item.text),
      vmid: this.selectedTreeNode.id
    });
  }


  editSelect(args: ClickEventArgs) {
    this.doJob.emit({
      type: (this.selectedTreeNode.type == NodeType.ServerGroup || this.selectedTreeNode.type == NodeType.Server) ? JobType.Server : JobType.LoadBalancer,
      action: Job.findJobAction(args.item.text),
      vmid: this.selectedTreeNode.id
    });
  }
}
