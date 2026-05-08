/**
 * useApi.js
 * Generic custom hook for calling any service function with managed
 * loading / error / data state.
 *
 * Usage:
 *   const { data, loading, error, execute } = useApi(studentService.getDashboard);
 *   useEffect(() => { execute(); }, []);
 *
 * Or with arguments:
 *   const { data, loading, execute } = useApi(adminService.getStudentById);
 *   execute(studentId);
 */
import { useState, useCallback } from 'react';

/**
 * @template T
 * @param {(...args: any[]) => Promise<{ data: T }>} serviceFunction
 * @param {{ immediate?: boolean, initialData?: T }} [options]
 */
const useApi = (serviceFunction, { immediate = false, initialData = null } = {}) => {
  const [data, setData]       = useState(initialData);
  const [loading, setLoading] = useState(immediate);
  const [error, setError]     = useState(null);

  const execute = useCallback(async (...args) => {
    setLoading(true);
    setError(null);
    try {
      const response = await serviceFunction(...args);
      const result = response?.data ?? response;
      setData(result);
      return { success: true, data: result };
    } catch (err) {
      const message =
        err?.response?.data?.message ||
        err?.message ||
        'Something went wrong. Please try again.';
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  }, [serviceFunction]); // eslint-disable-line react-hooks/exhaustive-deps

  const reset = useCallback(() => {
    setData(initialData);
    setError(null);
    setLoading(false);
  }, [initialData]);

  return { data, loading, error, execute, reset };
};

export default useApi;
