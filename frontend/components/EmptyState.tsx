export default function EmptyState({ title, subtitle }: { title: string; subtitle?: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center gap-2 border-2 border-dashed border-stone-400 dark:border-stone-700 bg-retro-card/60 dark:bg-stone-900/60 rounded-lg p-8">
      <div className="text-3xl mb-1">📻</div>
      <p className="text-retro-ink dark:text-stone-100 font-bold font-sans text-base">{title}</p>
      {subtitle && <p className="text-stone-500 dark:text-stone-400 font-mono text-xs max-w-sm">{subtitle}</p>}
    </div>
  );
}


