// composables/useApiFetch.ts
import { useAsyncData } from '#app';
import { $api } from '~/utils/api'; // Importamos nosso interceptor
import type { UseAsyncDataOptions } from '#app';
import type { FetchOptions } from 'ofetch';
import { unref } from 'vue';

/**
 * Combinamos os tipos de opções do useAsyncData e do FetchOptions
 * para que nosso composable aceite todos eles.
 */
type MyApiFetchOptions<T> = UseAsyncDataOptions<T> & FetchOptions;

/**
 * Este composable agora usa 'useAsyncData' para envolver
 * nossa instância '$api' personalizada.
 */
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

    // Se o path for nulo, não fazemos a chamada e retornamos nulo.
    // Isso é o que causa o tipo 'Promise<null>'
    if (!unwrappedPath) {
      return Promise.resolve(null);
    }

    const finalFetchOptions = { ...fetchOptions };
    if (finalFetchOptions.query) {
      finalFetchOptions.query = unref(finalFetchOptions.query);
    }
    
    // Se o path existir, retornamos 'Promise<T>'
    return $api<T>(
      unwrappedPath, 
      finalFetchOptions
    );
  };

  // --- A CORREÇÃO ESTÁ AQUI ---
  //
  // Em vez de 'useAsyncData<T>', nós usamos 'useAsyncData<T | null>'.
  // Agora o Nuxt espera um handler que retorne 'Promise<T | null>',
  // o que bate perfeitamente com o nosso handler.
  return useAsyncData<T | null>(asyncDataKey as string, handler, asyncDataOptions);
}