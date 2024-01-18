export interface IDocker {
  id: number;
  username?: string | null;
  repoName?: string | null;
  url?: string | null;
}

export type NewDocker = Omit<IDocker, 'id'> & { id: null };
