import { KesPipe } from './kes.pipe';

describe('KesPipe', () => {
  const pipe = new KesPipe();

  it('formats whole shillings with thousands separators', () => {
    expect(pipe.transform(24500)).toBe('KSh 24,500');
    expect(pipe.transform(1234567.6)).toBe('KSh 1,234,568');
  });

  it('returns an empty string for missing values', () => {
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
  });
});
