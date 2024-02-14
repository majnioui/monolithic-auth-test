export interface IInstanaApiToken {
  id: number;
  token?: string | null;
  url?: string | null;
}

export type NewInstanaApiToken = Omit<IInstanaApiToken, 'id'> & { id: null };
