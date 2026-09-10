import { definePreset } from '@primeuix/themes';
import Lara from '@primeuix/themes/lara';

/**
 * Brand preset built on top of PrimeNG's Lara theme.
 *
 * Keep the #004B99 brand ramp in sync with the legacy CSS tokens in
 * `_design-tokens.scss` so PrimeNG and utility classes share the same colors.
 */
export const InvaiPreset = definePreset(Lara, {
  semantic: {
    primary: {
      50: '#f2f6fa',
      100: '#ccdbeb',
      200: '#99b7d6',
      300: '#6693c2',
      400: '#336fad',
      500: '#004b99',
      600: '#003c7a',
      700: '#002d5c',
      800: '#001e3d',
      900: '#000f1f',
      950: '#00080f',
    },
    colorScheme: {
      light: {
        surface: {
          300: '#d1d5db',
        },
        formField: {
          borderColor: '{surface.300}',
          disabledBackground: '{surface.100}',
        },
        primary: {
          color: '{primary.500}',
          contrastColor: '#ffffff',
          hoverColor: '{primary.700}',
          activeColor: '{primary.800}',
        },
        highlight: {
          background: '{primary.50}',
          focusBackground: '{primary.50}',
          color: '{primary.800}',
          focusColor: '{primary.800}',
        },
      },
    },
  },
  components: {
    dialog: {
      header: { padding: '6px 24px' },
      content: { padding: '24px' },
      footer: { padding: '12px 24px' },
    },
    datatable: {
      colorScheme: {
        light: {
          row: {
            background: '#f9fafb',
            stripedBackground: '#ffffff',
            hoverBackground: '#f3f4f6',
          },
        },
      },
    },
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
