import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IGitrep } from '../gitrep.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../gitrep.test-samples';

import { GitrepService } from './gitrep.service';

const requireRestSample: IGitrep = {
  ...sampleWithRequiredData,
};

describe('Gitrep Service', () => {
  let service: GitrepService;
  let httpMock: HttpTestingController;
  let expectedResult: IGitrep | IGitrep[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(GitrepService);
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

    it('should create a Gitrep', () => {
      const gitrep = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(gitrep).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Gitrep', () => {
      const gitrep = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(gitrep).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Gitrep', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Gitrep', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Gitrep', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addGitrepToCollectionIfMissing', () => {
      it('should add a Gitrep to an empty array', () => {
        const gitrep: IGitrep = sampleWithRequiredData;
        expectedResult = service.addGitrepToCollectionIfMissing([], gitrep);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(gitrep);
      });

      it('should not add a Gitrep to an array that contains it', () => {
        const gitrep: IGitrep = sampleWithRequiredData;
        const gitrepCollection: IGitrep[] = [
          {
            ...gitrep,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addGitrepToCollectionIfMissing(gitrepCollection, gitrep);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Gitrep to an array that doesn't contain it", () => {
        const gitrep: IGitrep = sampleWithRequiredData;
        const gitrepCollection: IGitrep[] = [sampleWithPartialData];
        expectedResult = service.addGitrepToCollectionIfMissing(gitrepCollection, gitrep);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(gitrep);
      });

      it('should add only unique Gitrep to an array', () => {
        const gitrepArray: IGitrep[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const gitrepCollection: IGitrep[] = [sampleWithRequiredData];
        expectedResult = service.addGitrepToCollectionIfMissing(gitrepCollection, ...gitrepArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const gitrep: IGitrep = sampleWithRequiredData;
        const gitrep2: IGitrep = sampleWithPartialData;
        expectedResult = service.addGitrepToCollectionIfMissing([], gitrep, gitrep2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(gitrep);
        expect(expectedResult).toContain(gitrep2);
      });

      it('should accept null and undefined values', () => {
        const gitrep: IGitrep = sampleWithRequiredData;
        expectedResult = service.addGitrepToCollectionIfMissing([], null, gitrep, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(gitrep);
      });

      it('should return initial array if no Gitrep is added', () => {
        const gitrepCollection: IGitrep[] = [sampleWithRequiredData];
        expectedResult = service.addGitrepToCollectionIfMissing(gitrepCollection, undefined, null);
        expect(expectedResult).toEqual(gitrepCollection);
      });
    });

    describe('compareGitrep', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareGitrep(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareGitrep(entity1, entity2);
        const compareResult2 = service.compareGitrep(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareGitrep(entity1, entity2);
        const compareResult2 = service.compareGitrep(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareGitrep(entity1, entity2);
        const compareResult2 = service.compareGitrep(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
