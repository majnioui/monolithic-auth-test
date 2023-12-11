import { IClient, NewClient } from './client.model';

export const sampleWithRequiredData: IClient = {
  id: 1288,
};

export const sampleWithPartialData: IClient = {
  id: 6230,
  clientname: 'which gadzooks greedy',
};

export const sampleWithFullData: IClient = {
  id: 24537,
  orgname: 'weakly notwithstanding',
  clientname: 'sting among boastfully',
};

export const sampleWithNewData: NewClient = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
