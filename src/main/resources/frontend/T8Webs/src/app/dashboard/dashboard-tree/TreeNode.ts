
export class TreeNode {
  id: number = -1;
  name: string = '';
  status: string = '';
  type: NodeType = NodeType.None

  constructor(id: number, name: string, status: string, type: NodeType) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.type = type;
  }
}

export class NodeType {
  public static readonly ServerGroup = new NodeType(0, 'Servers');
  public static readonly Server = new NodeType(1, 'Server');
  public static readonly BalancerGroup = new NodeType(2, 'Load Balancers');
  public static readonly LoadBalancer = new NodeType(3, 'Load Balancer');
  public static readonly None = new NodeType(4, 'Dashboard');

  private constructor(public readonly id: number, public readonly displayName: string) { }

  static findNodeTypeById(typeId: number | null): NodeType {
    switch (typeId) {
      case NodeType.ServerGroup.id: return NodeType.ServerGroup;
      case NodeType.BalancerGroup.id: return NodeType.BalancerGroup
      case NodeType.Server.id: return NodeType.Server;
      case NodeType.LoadBalancer.id: return NodeType.LoadBalancer;
      default: return NodeType.None;
    }
  }

  static findNodeTypeByName(typeName: string | null): NodeType {
    switch (typeName) {
      case NodeType.ServerGroup.displayName: return NodeType.ServerGroup;
      case NodeType.BalancerGroup.displayName: return NodeType.BalancerGroup
      case NodeType.Server.displayName: return NodeType.Server;
      case NodeType.LoadBalancer.displayName: return NodeType.LoadBalancer;
      default: return NodeType.None;
    }
  }
}
