import { IGitrep, NewGitrep } from './gitrep.model';

export const sampleWithRequiredData: IGitrep = {
  id: 14841,
  accesstoken: 'alongside boohoo what',
};

export const sampleWithPartialData: IGitrep = {
  id: 20728,
  clientid: 'bah',
  accesstoken: 'mix short',
};

export const sampleWithFullData: IGitrep = {
  id: 5392,
  clientid: 'barge wrongly',
  accesstoken: 'ruck er',
};

export const sampleWithNewData: NewGitrep = {
  accesstoken: 'only not sweetly',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
