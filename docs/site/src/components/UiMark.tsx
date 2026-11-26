import type { ReactNode } from 'react';
import Box from '@mui/material/Box';

/** Penanda untuk kalimat atau nama komponen yang terlihat di aplikasi. */
export default function UiMark({ children }: { children: ReactNode }) {
  return (
    <Box
      component="span"
      sx={{
        color: 'primary.main',
        bgcolor: '#F3E8FF',
        px: 0.5,
        py: 0.1,
        borderRadius: 0.75,
        fontWeight: 600,
      }}
    >
      {children}
    </Box>
  );
}
