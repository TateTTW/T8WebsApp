
export interface User {
  name: string;
  picture: string;
  status: string;
}

export enum UserStatus {
  NONE = "NONE",
  REQUESTED = "REQUESTED",
  APPROVED = "APPROVED",
  ADMIN = "ADMIN",
}
