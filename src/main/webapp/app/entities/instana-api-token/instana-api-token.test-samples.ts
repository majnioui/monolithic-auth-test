import { IInstanaApiToken, NewInstanaApiToken } from './instana-api-token.model';

export const sampleWithRequiredData: IInstanaApiToken = {
  id: 10152,
};

export const sampleWithPartialData: IInstanaApiToken = {
  id: 21915,
};

export const sampleWithFullData: IInstanaApiToken = {
  id: 26287,
  token: 'breakable bind',
};

export const sampleWithNewData: NewInstanaApiToken = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
