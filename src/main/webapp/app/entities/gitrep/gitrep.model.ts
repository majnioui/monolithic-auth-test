import { IClient } from 'app/entities/client/client.model';

export interface IGitrep {
  id: number;
  clientid?: string | null;
  accesstoken?: string | null;
  platformType?: 'GitHub' | 'GitLab';
  client?: Pick<IClient, 'id'> | null;
}

export type NewGitrep = Omit<IGitrep, 'id'> & { id: null };
