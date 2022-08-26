
export class TreeNode {
  id: number = -1;
  name: string = '';
  status: string = '';
  type: NodeType = NodeType.NONE

  constructor(id: number, name: string, status: string, type: NodeType) {
    this.id = id;
    this.name = name;
    this.status = status;
    this.type = type;
  }
}

export class NodeType {
  public static readonly NONE = new NodeType(0, 'Dashboard');
  public static readonly ADMIN_GROUP = new NodeType(1, 'System');
  public static readonly ADMIN_USERS = new NodeType(2, 'Users');
  public static readonly ADMIN_SERVERS = new NodeType(3, 'Servers');
  public static readonly SERVER_GROUP = new NodeType(4, 'Servers');
  public static readonly SERVER = new NodeType(5, 'Server');
  public static readonly BALANCER_GROUP = new NodeType(6, 'Load Balancers');
  public static readonly LOAD_BALANCER = new NodeType(7, 'Load Balancer');


  private constructor(public readonly id: number, public readonly displayName: string) { }

  static findNodeTypeById(typeId: number | null): NodeType {
    switch (typeId) {
      case NodeType.ADMIN_GROUP.id: return NodeType.ADMIN_GROUP;
      case NodeType.ADMIN_USERS.id: return NodeType.ADMIN_USERS;
      case NodeType.ADMIN_SERVERS.id: return NodeType.ADMIN_SERVERS;
      case NodeType.SERVER_GROUP.id: return NodeType.SERVER_GROUP;
      case NodeType.BALANCER_GROUP.id: return NodeType.BALANCER_GROUP
      case NodeType.SERVER.id: return NodeType.SERVER;
      case NodeType.LOAD_BALANCER.id: return NodeType.LOAD_BALANCER;
      default: return NodeType.NONE;
    }
  }

  static findNodeTypeByName(typeName: string | null): NodeType {
    switch (typeName) {
      case NodeType.SERVER_GROUP.displayName: return NodeType.SERVER_GROUP;
      case NodeType.BALANCER_GROUP.displayName: return NodeType.BALANCER_GROUP
      case NodeType.SERVER.displayName: return NodeType.SERVER;
      case NodeType.LOAD_BALANCER.displayName: return NodeType.LOAD_BALANCER;
      default: return NodeType.NONE;
    }
  }
}
