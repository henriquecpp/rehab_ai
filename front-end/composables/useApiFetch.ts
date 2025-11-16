// composables/useApiFetch.ts
import { useAsyncData } from '#app';
import { $api } from '~/utils/api'; // Importamos nosso interceptor
import type { UseAsyncDataOptions } from '#app';
import type { FetchOptions } from 'ofetch';
import { unref } from 'vue';

type MyApiFetchOptions<T> = UseAsyncDataOptions<T> & FetchOptions;

export function useApiFetch<T>(
  path: string | globalThis.ComputedRef<string | null>, 
  options: MyApiFetchOptions<T> = {}
) {
  
  const { 
    lazy, 
    server, 
    key, 
    default: defaultValue, 
    watch, 
    immediate,
    ...fetchOptions 
  } = options;

  const asyncDataOptions = {
    lazy,
    server,
    default: defaultValue,
    watch,
    immediate,
  };

  const asyncDataKey = unref(key) || unref(path);

  const handler = () => {
    const unwrappedPath = unref(path);
    if (!unwrappedPath) {
      return Promise.resolve(null);
    }

    const finalFetchOptions = { ...fetchOptions };
    if (finalFetchOptions.query) {
      finalFetchOptions.query = unref(finalFetchOptions.query);
    }
    return $api<T>(
      unwrappedPath, 
      finalFetchOptions
    );
  };

  return useAsyncData<T | null>(asyncDataKey as string, handler, asyncDataOptions);
}