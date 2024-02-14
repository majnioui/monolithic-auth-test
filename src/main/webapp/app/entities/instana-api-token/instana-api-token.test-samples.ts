import { IInstanaApiToken, NewInstanaApiToken } from './instana-api-token.model';

export const sampleWithRequiredData: IInstanaApiToken = {
  id: 17106,
};

export const sampleWithPartialData: IInstanaApiToken = {
  id: 29314,
};

export const sampleWithFullData: IInstanaApiToken = {
  id: 23249,
  token: 'twin primary',
  url: 'https://quarterly-interface.org/',
};

export const sampleWithNewData: NewInstanaApiToken = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
