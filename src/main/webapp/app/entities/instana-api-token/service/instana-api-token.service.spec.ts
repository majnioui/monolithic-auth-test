import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IInstanaApiToken } from '../instana-api-token.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../instana-api-token.test-samples';

import { InstanaApiTokenService } from './instana-api-token.service';

const requireRestSample: IInstanaApiToken = {
  ...sampleWithRequiredData,
};

describe('InstanaApiToken Service', () => {
  let service: InstanaApiTokenService;
  let httpMock: HttpTestingController;
  let expectedResult: IInstanaApiToken | IInstanaApiToken[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(InstanaApiTokenService);
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

    it('should create a InstanaApiToken', () => {
      const instanaApiToken = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(instanaApiToken).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a InstanaApiToken', () => {
      const instanaApiToken = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(instanaApiToken).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a InstanaApiToken', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of InstanaApiToken', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a InstanaApiToken', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addInstanaApiTokenToCollectionIfMissing', () => {
      it('should add a InstanaApiToken to an empty array', () => {
        const instanaApiToken: IInstanaApiToken = sampleWithRequiredData;
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing([], instanaApiToken);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(instanaApiToken);
      });

      it('should not add a InstanaApiToken to an array that contains it', () => {
        const instanaApiToken: IInstanaApiToken = sampleWithRequiredData;
        const instanaApiTokenCollection: IInstanaApiToken[] = [
          {
            ...instanaApiToken,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing(instanaApiTokenCollection, instanaApiToken);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a InstanaApiToken to an array that doesn't contain it", () => {
        const instanaApiToken: IInstanaApiToken = sampleWithRequiredData;
        const instanaApiTokenCollection: IInstanaApiToken[] = [sampleWithPartialData];
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing(instanaApiTokenCollection, instanaApiToken);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(instanaApiToken);
      });

      it('should add only unique InstanaApiToken to an array', () => {
        const instanaApiTokenArray: IInstanaApiToken[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const instanaApiTokenCollection: IInstanaApiToken[] = [sampleWithRequiredData];
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing(instanaApiTokenCollection, ...instanaApiTokenArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const instanaApiToken: IInstanaApiToken = sampleWithRequiredData;
        const instanaApiToken2: IInstanaApiToken = sampleWithPartialData;
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing([], instanaApiToken, instanaApiToken2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(instanaApiToken);
        expect(expectedResult).toContain(instanaApiToken2);
      });

      it('should accept null and undefined values', () => {
        const instanaApiToken: IInstanaApiToken = sampleWithRequiredData;
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing([], null, instanaApiToken, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(instanaApiToken);
      });

      it('should return initial array if no InstanaApiToken is added', () => {
        const instanaApiTokenCollection: IInstanaApiToken[] = [sampleWithRequiredData];
        expectedResult = service.addInstanaApiTokenToCollectionIfMissing(instanaApiTokenCollection, undefined, null);
        expect(expectedResult).toEqual(instanaApiTokenCollection);
      });
    });

    describe('compareInstanaApiToken', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareInstanaApiToken(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareInstanaApiToken(entity1, entity2);
        const compareResult2 = service.compareInstanaApiToken(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareInstanaApiToken(entity1, entity2);
        const compareResult2 = service.compareInstanaApiToken(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareInstanaApiToken(entity1, entity2);
        const compareResult2 = service.compareInstanaApiToken(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
