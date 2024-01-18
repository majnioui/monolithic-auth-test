import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IDocker } from '../docker.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../docker.test-samples';

import { DockerService } from './docker.service';

const requireRestSample: IDocker = {
  ...sampleWithRequiredData,
};

describe('Docker Service', () => {
  let service: DockerService;
  let httpMock: HttpTestingController;
  let expectedResult: IDocker | IDocker[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(DockerService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Docker', () => {
      const docker = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(docker).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Docker', () => {
      const docker = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(docker).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Docker', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Docker', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Docker', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addDockerToCollectionIfMissing', () => {
      it('should add a Docker to an empty array', () => {
        const docker: IDocker = sampleWithRequiredData;
        expectedResult = service.addDockerToCollectionIfMissing([], docker);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(docker);
      });

      it('should not add a Docker to an array that contains it', () => {
        const docker: IDocker = sampleWithRequiredData;
        const dockerCollection: IDocker[] = [
          {
            ...docker,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addDockerToCollectionIfMissing(dockerCollection, docker);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Docker to an array that doesn't contain it", () => {
        const docker: IDocker = sampleWithRequiredData;
        const dockerCollection: IDocker[] = [sampleWithPartialData];
        expectedResult = service.addDockerToCollectionIfMissing(dockerCollection, docker);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(docker);
      });

      it('should add only unique Docker to an array', () => {
        const dockerArray: IDocker[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const dockerCollection: IDocker[] = [sampleWithRequiredData];
        expectedResult = service.addDockerToCollectionIfMissing(dockerCollection, ...dockerArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const docker: IDocker = sampleWithRequiredData;
        const docker2: IDocker = sampleWithPartialData;
        expectedResult = service.addDockerToCollectionIfMissing([], docker, docker2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(docker);
        expect(expectedResult).toContain(docker2);
      });

      it('should accept null and undefined values', () => {
        const docker: IDocker = sampleWithRequiredData;
        expectedResult = service.addDockerToCollectionIfMissing([], null, docker, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(docker);
      });

      it('should return initial array if no Docker is added', () => {
        const dockerCollection: IDocker[] = [sampleWithRequiredData];
        expectedResult = service.addDockerToCollectionIfMissing(dockerCollection, undefined, null);
        expect(expectedResult).toEqual(dockerCollection);
      });
    });

    describe('compareDocker', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareDocker(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareDocker(entity1, entity2);
        const compareResult2 = service.compareDocker(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareDocker(entity1, entity2);
        const compareResult2 = service.compareDocker(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareDocker(entity1, entity2);
        const compareResult2 = service.compareDocker(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
