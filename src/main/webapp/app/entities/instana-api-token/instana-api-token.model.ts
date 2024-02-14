export interface IInstanaApiToken {
  id: number;
  token?: string | null;
}

export type NewInstanaApiToken = Omit<IInstanaApiToken, 'id'> & { id: null };
