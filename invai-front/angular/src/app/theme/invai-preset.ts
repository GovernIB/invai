import { definePreset } from '@primeuix/themes';
import Lara from '@primeuix/themes/lara';

/**
 * Brand preset built on top of PrimeNG's Lara theme.
 *
 * The design values (primary blue ramp, contrast/highlight colors) are taken
 * from the legacy SCSS configuration under `src/app/theme` so the new
 * token-based theming engine renders the same brand identity.
 */
export const InvaiPreset = definePreset(Lara, {
  semantic: {
    primary: {
      50: '#f3f8ff',
      100: '#ccdefb',
      200: '#99bdf7',
      300: '#669cf3',
      400: '#337bef',
      500: '#005aeb',
      600: '#0048bc',
      700: '#00368d',
      800: '#00245e',
      900: '#00122f',
      950: '#000918',
    },
    colorScheme: {
      light: {
        formField: {
          disabledBackground: '{surface.100}',
        },
        primary: {
          color: '{primary.500}',
          contrastColor: '#ffffff',
          hoverColor: '{primary.700}',
          activeColor: '{primary.800}',
        },
        highlight: {
          background: '#eff6ff',
          focusBackground: '#eff6ff',
          color: '{primary.800}',
          focusColor: '{primary.800}',
        },
      },
    },
  },
  components: {
    button: {
      colorScheme: {
        light: {
          root: {
            success: {
              background: '#178740',
              hoverBackground: '#136d34',
              activeBackground: '#0e5327',
              borderColor: '#178740',
              hoverBorderColor: '#136d34',
              activeBorderColor: '#0e5327',
              color: '#ffffff',
              hoverColor: '#ffffff',
              activeColor: '#ffffff',
              focusRing: {
                color: 'transparent',
                shadow: '0 0 0 0.2rem #c2f5d5',
              },
            },
            danger: {
              background: '#e11313',
              hoverBackground: '#b30f0f',
              activeBackground: '#880c0c',
              borderColor: '#e11313',
              hoverBorderColor: '#b30f0f',
              activeBorderColor: '#880c0c',
              color: '#ffffff',
              hoverColor: '#ffffff',
              activeColor: '#ffffff',
              focusRing: {
                color: 'transparent',
                shadow: '0 0 0 0.2rem #fbd0d0',
              },
            },
          },
          outlined: {
            success: {
              hoverBackground: '#22c55e0a',
              activeBackground: '#22c55e29',
              borderColor: '#178740',
              color: '#178740',
            },
            danger: {
              hoverBackground: '#ef44440a',
              activeBackground: '#ef444429',
              borderColor: '#e11313',
              color: '#e11313',
            },
          },
        },
      },
    },
  },
});
