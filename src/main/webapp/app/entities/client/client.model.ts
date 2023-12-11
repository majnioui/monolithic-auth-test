export interface IClient {
  id: number;
  orgname?: string | null;
  clientname?: string | null;
}

export type NewClient = Omit<IClient, 'id'> & { id: null };
