import { InvaiPreset } from './invai-preset';

describe('InvaiPreset', () => {
  it('uses the legacy surface.300 boundary and surface.100 only for disabled fields', () => {
    const light = (
      InvaiPreset.semantic as {
        colorScheme?: {
          light?: {
            surface?: Record<number, string>;
            formField?: Record<string, string>;
          };
        };
      }
    )?.colorScheme?.light;

    expect(light?.surface?.[300]).toBe('#d1d5db');
    expect(light?.formField?.['borderColor']).toBe('{surface.300}');
    expect(light?.formField?.['disabledBackground']).toBe('{surface.100}');
  });

  it('keeps odd table rows white and even rows lighter than hover', () => {
    const lightRowTokens = InvaiPreset.components?.datatable?.colorScheme?.light?.row;

    expect(lightRowTokens).toEqual({
      background: '#f9fafb',
      stripedBackground: '#ffffff',
      hoverBackground: '#f3f4f6',
    });
  });

  it('defines the branded success and danger button states', () => {
    const lightButtonTokens = InvaiPreset.components?.button?.colorScheme?.light;

    expect(lightButtonTokens?.root?.success).toEqual({
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
    });
    expect(lightButtonTokens?.outlined?.success).toEqual({
      hoverBackground: '#22c55e0a',
      activeBackground: '#22c55e29',
      borderColor: '#178740',
      color: '#178740',
    });
    expect(lightButtonTokens?.root?.danger).toEqual({
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
    });
    expect(lightButtonTokens?.outlined?.danger).toEqual({
      hoverBackground: '#ef44440a',
      activeBackground: '#ef444429',
      borderColor: '#e11313',
      color: '#e11313',
    });
  });
});
