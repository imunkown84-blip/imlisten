'use client';

export default function StarRating({
  value,
  onChange
}: {
  value: number | null;
  onChange: (rating: number) => void;
}) {
  return (
    <div className="flex gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <button
          key={star}
          type="button"
          onClick={() => onChange(star)}
          className={`text-lg leading-none transition-transform hover:scale-125 ${
            value && star <= value ? 'text-amber-500' : 'text-stone-300'
          }`}
          aria-label={`Rate ${star} stars`}
        >
          ★
        </button>
      ))}
    </div>

  );
}
