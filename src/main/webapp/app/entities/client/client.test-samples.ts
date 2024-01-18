import { IClient, NewClient } from './client.model';

export const sampleWithRequiredData: IClient = {
  id: 13694,
};

export const sampleWithPartialData: IClient = {
  id: 25425,
  orgname: 'possibility gah meanwhile',
};

export const sampleWithFullData: IClient = {
  id: 18750,
  orgname: 'consequently',
  clientname: 'hm honorable',
};

export const sampleWithNewData: NewClient = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
