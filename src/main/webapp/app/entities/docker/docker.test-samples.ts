import { IDocker, NewDocker } from './docker.model';

export const sampleWithRequiredData: IDocker = {
  id: 24870,
};

export const sampleWithPartialData: IDocker = {
  id: 31459,
  repoName: 'unhappy',
  url: 'https://outrageous-tights.com/',
};

export const sampleWithFullData: IDocker = {
  id: 5005,
  username: 'perfectly',
  repoName: 'phooey',
  url: 'https://wise-homeland.org/',
};

export const sampleWithNewData: NewDocker = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
